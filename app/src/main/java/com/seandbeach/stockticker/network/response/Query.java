
package com.seandbeach.stockticker.network.response;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Query {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("created")
    @Expose
    private String created;
    @SerializedName("lang")
    @Expose
    private String lang;
    @SerializedName("results")
    @Expose
    private Results results;

    /**
     * @return The count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count The count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return The created
     */
    public String getCreated() {
        return created;
    }

    /**
     * @param created The created
     */
    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * @return The lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * @param lang The lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * @return The results
     */
    public Results getResults() {
        return results;
    }

    /**
     * @param results The results
     */
    public void setResults(Results results) {
        this.results = results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Query)) {
            return false;
        }

        Query query_ = (Query) o;

        if (count != query_.count) {
            return false;
        }
        if (created != null ? !created.equals(query_.created) : query_.created != null) {
            return false;
        }
        if (lang != null ? !lang.equals(query_.lang) : query_.lang != null) {
            return false;
        }
        if (results != null ? !results.equals(query_.results) : query_.results != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = count;
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (lang != null ? lang.hashCode() : 0);
        result = 31 * result + (results != null ? results.hashCode() : 0);
        return result;
    }
}
