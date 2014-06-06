package org.dcm4chee.xds2.tool;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryRequest;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryResponse;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryType;
import org.dcm4chee.xds2.infoset.rim.IdentifiableType;
import org.dcm4chee.xds2.infoset.rim.ObjectFactory;
import org.dcm4chee.xds2.infoset.rim.RegistryObjectListType;
import org.dcm4chee.xds2.infoset.rim.ResponseOptionType;
import org.dcm4chee.xds2.infoset.rim.SlotType1;
import org.dcm4chee.xds2.infoset.rim.SubmitObjectsRequest;
import org.dcm4chee.xds2.infoset.rim.ValueListType;
import org.dcm4chee.xds2.infoset.util.DocumentRegistryPortTypeFactory;
import org.dcm4chee.xds2.infoset.ws.registry.DocumentRegistryPortType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Reg2RegMigration {

    private static Logger log = LoggerFactory.getLogger(Reg2RegMigration.class);

    @Test
    public void migrateDataAsTest() throws JAXBException {
        String[] patids = {"XDS-Pat1^^^&1.2.3.4.5.99&ISO"};
        //XDS-Pat1^^^&1.2.3.4.5.99&ISO
        //John^^^&1.2.3.4.5&ISO
        //http://10.231.161.57:8180/dcm4chee-xds/XDSbRegistry/b
        //http://localhost:8080/xds/registry
        migrateData(Arrays.asList(patids), "http://10.231.161.57:8180/dcm4chee-xds/XDSbRegistry/b", "http://localhost:8080/xds/registry");
    }

    public void migrateData(List<String> patientIds, String srcRegistryQueryEndpointURL, String destRegistryRegisterEndpointURL) throws JAXBException {

        ObjectFactory objectFactory = new ObjectFactory();

        DocumentRegistryPortType portSrc = DocumentRegistryPortTypeFactory.getDocumentRegistryPortSoap12(srcRegistryQueryEndpointURL);
        DocumentRegistryPortType portDst = DocumentRegistryPortTypeFactory.getDocumentRegistryPortSoap12(destRegistryRegisterEndpointURL);

        
        // create request
        SubmitObjectsRequest submitAllRequest = new SubmitObjectsRequest();
        submitAllRequest.setRegistryObjectList(new RegistryObjectListType());
        
        
        //submitAllRequest = readResourceSubmitObjectsRequest();
        
        for (String pId : patientIds) {
                log.info("Getting list of submission sets for patient {} ..", pId);

                AdhocQueryResponse rsp = portSrc.documentRegistryRegistryStoredQuery(getAllSubmissionSetsRequest(pId));
                
                for (JAXBElement<? extends IdentifiableType> submSetElem : rsp.getRegistryObjectList().getIdentifiable()) {
                    IdentifiableType submSet = submSetElem.getValue();
                    log.info("identifiable (submission set) with id {} . Contents:", submSet.getId());
                    
                    AdhocQueryResponse submSetContentsRsp = portSrc.documentRegistryRegistryStoredQuery(getSubmissionSetRequest(submSet.getId())); 

                    for (JAXBElement<? extends IdentifiableType> submSetContentElem : submSetContentsRsp.getRegistryObjectList().getIdentifiable()) {
                        IdentifiableType i = submSetContentElem.getValue();
                        log.info("identifiable with id {} ", i.getId());
                    }
                       
                    submitAllRequest.getRegistryObjectList().getIdentifiable().addAll(submSetContentsRsp.getRegistryObjectList().getIdentifiable());
                    
                };
                
        }
        
        /*JAXBContext jaxbContext = JAXBContext.newInstance(SubmitObjectsRequest.class);
        Marshaller marshaller = jaxbContext.createMarshaller();        
        StringWriter s = new StringWriter();
        marshaller.marshal(submitAllRequest, s); 
        System.out.println(s.getBuffer().toString());*/        
        
        

        portDst.documentRegistryRegisterDocumentSetB(submitAllRequest);

    }
    
    private SubmitObjectsRequest readResourceSubmitObjectsRequest() throws JAXBException {
        
        JAXBContext jaxbContext = JAXBContext.newInstance(SubmitObjectsRequest.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Marshaller marshaller = jaxbContext.createMarshaller();
        SubmitObjectsRequest req = (SubmitObjectsRequest) unmarshaller.unmarshal(
                Reg2RegMigration.class.getResourceAsStream("registerObjectsRequest.xml"));
        
        return req;
                
    }
    
    
    
    private AdhocQueryRequest getSubmissionSetRequest(String uuid) {
        AdhocQueryRequest req = new AdhocQueryRequest();
        
        // query
        AdhocQueryType query = new AdhocQueryType();

        query.setId(XDSConstants.XDS_GetSubmissionSetAndContents);
        
        // resp option
        ResponseOptionType responseOption = new ResponseOptionType();
        responseOption.setReturnType("LeafClass");
        responseOption.setReturnComposedObjects(true);
        req.setResponseOption(responseOption);
        
        // subm set uuid
        SlotType1 submSetUuid = new SlotType1();
        submSetUuid.setName(XDSConstants.QRY_SUBMISSIONSET_ENTRY_UUID);
        ValueListType uuidValues = new ValueListType();
        uuidValues.getValue().add(uuid);
        submSetUuid.setValueList(uuidValues);
        query.getSlot().add(submSetUuid);        
        
        req.setAdhocQuery(query); 
        
        return req;
    }
    
    private AdhocQueryRequest getAllSubmissionSetsRequest(String pId) {
        AdhocQueryRequest req = new AdhocQueryRequest();
        
        // query
        AdhocQueryType query = new AdhocQueryType();

        query.setId(XDSConstants.XDS_FindSubmissionSets);

        // resp option
        ResponseOptionType responseOption = new ResponseOptionType();
        responseOption.setReturnType("ObjectRef");
        responseOption.setReturnComposedObjects(false);
//        responseOption.setReturnType("LeafClass");
        //responseOption.setReturnComposedObjects(true);
        req.setResponseOption(responseOption);
        
        // pid
        SlotType1 slotPid = new SlotType1();
        slotPid.setName(XDSConstants.QRY_SUBMISSIONSET_PATIENT_ID);
        ValueListType pidValues = new ValueListType();
        pidValues.getValue().add(pId);
        slotPid.setValueList(pidValues);
        query.getSlot().add(slotPid);

        // status
        SlotType1 slotStatus = new SlotType1();
        slotStatus.setName(XDSConstants.QRY_SUBMISSIONSET_STATUS);
        ValueListType statusValues = new ValueListType();
        statusValues.getValue().add("('"+XDSConstants.STATUS_APPROVED+"')");
        statusValues.getValue().add("('"+XDSConstants.STATUS_DEPRECATED+"')");
        statusValues.getValue().add("('"+XDSConstants.STATUS_SUBMITTED+"')");
        slotStatus.setValueList(statusValues);
        query.getSlot().add(slotStatus);

        req.setAdhocQuery(query);

        return req;
        
    }
}
