/*	
 * MutationHandler.java	
 *	
 * Created on 12. Januar 2006, 14:11	
 */	
	
package com.eidp.webctrl.modules.ESID.Mutation;	
	
import java.util.HashMap;	
import java.util.Vector;	
import javax.ejb.Handle;	
import com.eidp.core.DB.DBMappingRemote;	
import com.eidp.UserScopeObject.UserScopeObject;	
	
import java.io.IOException;	
import java.rmi.RemoteException;	
import java.sql.SQLException;	
import org.xml.sax.SAXException;	
	
/**	
 *	
 * @author  david	
 */	
public class MutationHandler {	
    	
    private HashMap results;	
    private Vector mutations;	
    private String pubID;	
    private String gene;	
    private String mutseq;	
    private String refseq;	
    private String seqtype;	
    private String mutid;	
    	
    /** Creates a new instance of MutationHandler */	
    public MutationHandler() {	
    }	
    	
    public void runMutationQuery(UserScopeObject uso) throws MutationException {	
        HashMap paramMap = new HashMap();	
        HashMap patientID = new HashMap();	
        String PatientID = new String();     	
        try {	
            DBMappingRemote dbMapper = (DBMappingRemote)((Handle)uso.session.getAttribute("dbMapperHandle")).getEJBObject();	
            paramMap.put("id", this.getPubID());	
            dbMapper.DBAction("PATIENTLIST", "getPatientForID", paramMap);	
            patientID = uso.dbMapper.getRow(0);	
            PatientID = (String) patientID.get("patient_id");	
            paramMap.clear();	
            paramMap.put("patient_id", PatientID);	
            paramMap.put("gene", getGene());	
            paramMap.put("type", getSeqtype());	
            dbMapper.DBAction("MUTATION", "getMutationForPatientIDAndGeneAndType", paramMap); //must be sorted by init_pos	
            this.mutations = dbMapper.getRowRange(0, uso.dbMapper.size());	
        } catch (RemoteException e) {	
            throw new MutationException(e.getMessage());	
        } catch (SQLException f) {	
            throw new MutationException(f.getMessage());	
        } catch (SAXException g) {	
            throw new MutationException(g.getMessage());	
        } catch (IOException h) {	
            throw new MutationException(h.getMessage());	
        }	
    }	
    	
    	
    public void selectMutationPair(int mutid) {	
        this.results = (HashMap) mutations.elementAt(mutid);	
    }	
    	
    public void setPubID(String pubid) {	
        this.pubID = pubid;	
    }	
    	
    String getPubID() {	
        return(pubID);	
    }	
    	
    public void setGene(String genein) {	
        this.gene = genein;	
    }	
    	
    String getGene() {	
        return(gene);	
    }	
    	
    public void setSeqtype(String seqtype) {	
        this.seqtype = seqtype;	
    }	
    	
    String getSeqtype() {	
        return(seqtype);	
    }	
       	
    void setReferenceSequence(String referenseq) {	
        this.refseq = referenseq;	
    }	
    	
    public String getReferenceSequence() {	
        if (!results.isEmpty()) {	
            String reference = (String) this.results.get("refseq");	
            setReferenceSequence(reference);	
        }	
        return(refseq);	
    }	
    	
    void setMutationSequence(String mutatedseq) {        	
        this.mutseq = mutatedseq;	
    }	
    	
    public String getMutationSequence() {	
        if (!results.isEmpty()) {	
            String mutation = (String) this.results.get("mutseq");	
            setMutationSequence(mutation);	
        }	
        return(mutseq);	
    }	
      	
}	
