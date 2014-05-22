package org.dcm4chee.xds2.persistence;

import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.jxpath.JXPathContext;
import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.infoset.rim.ExtrinsicObjectType;
import org.dcm4chee.xds2.infoset.rim.SubmitObjectsRequest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathTest {

    private static Logger log = LoggerFactory.getLogger(XPathTest.class);
    
    @Test
    public void checkXpaths() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(SubmitObjectsRequest.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        SubmitObjectsRequest req = (SubmitObjectsRequest) unmarshaller.unmarshal(
                XPathTest.class.getResourceAsStream("RegisterOneDocument.xml")); 
        
/*        for (JAXBElement<? extends IdentifiableType> e : req.getRegistryObjectList().getIdentifiable()) {
            log.info("type is {}", e.getDeclaredType());
        }*/
        
        JAXBElement<ExtrinsicObjectType> el = (JAXBElement<ExtrinsicObjectType>) req.getRegistryObjectList().getIdentifiable().get(0);
        ExtrinsicObjectType obj = el.getValue();
        log.info("Id {}, ", obj.getId());
        
        JXPathContext context = JXPathContext.newContext(obj);
        String XDSDocUniqIdXpath = String.format("externalIdentifier[identificationScheme='%s']/value", XDSConstants.UUID_XDSDocumentEntry_uniqueId);
        
        String XDSDocEntryAuthorXpath = String.format("classification[classificationScheme='%s']/slot[name='%s']/valueList/value" ,XDSConstants.UUID_XDSDocumentEntry_author, XDSConstants.SLOT_NAME_AUTHOR_PERSON);
        String XDSSubmSetAuthorXpath = String.format("classification[classificationScheme='%s']/slot[name='%s']/valueList/value" ,XDSConstants.UUID_XDSSubmissionSet_autor, XDSConstants.SLOT_NAME_AUTHOR_PERSON);
        
        
        
        String uniqid = (String)context.getValue(XDSDocUniqIdXpath);
        Iterator author = (Iterator) context.iterate(XDSDocEntryAuthorXpath); 
        

//        List<ClassificationType> cl = (List<ClassificationType>) context.getValue(authorXpath1); 
        
        
        log.info("Uniq ID {}, ", uniqid);
        
        String auth;
        while (( auth = (String) author.next() ) != null) {
            log.info("Author {}, ", auth);
            
        }
  //      log.info("Author* {} ", cl);
        
    }
    
}
