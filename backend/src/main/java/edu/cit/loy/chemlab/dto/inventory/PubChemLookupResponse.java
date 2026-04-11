package edu.cit.loy.chemlab.dto.inventory;

public class PubChemLookupResponse {

    private Integer cid;
    private String formula;
    private String weight;
    private String iupac;
    private String source;

    public PubChemLookupResponse() {
    }

    public PubChemLookupResponse(Integer cid, String formula, String weight, String iupac, String source) {
        this.cid = cid;
        this.formula = formula;
        this.weight = weight;
        this.iupac = iupac;
        this.source = source;
    }

    public Integer getCid() { return cid; }
    public void setCid(Integer cid) { this.cid = cid; }

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getIupac() { return iupac; }
    public void setIupac(String iupac) { this.iupac = iupac; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
