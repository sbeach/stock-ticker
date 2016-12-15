
package com.seandbeach.stockticker.network.response;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Results {

    @SerializedName("quote")
    @Expose
    private List<Quote> quote = new ArrayList<Quote>();

    /**
     * @return The quote
     */
    public List<Quote> getQuote() {
        return quote;
    }

    /**
     * @param quote The quote
     */
    public void setQuote(List<Quote> quote) {
        this.quote = quote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Results)) {
            return false;
        }

        Results results = (Results) o;

        if (quote != null ? !quote.equals(results.quote) : results.quote != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return quote != null ? quote.hashCode() : 0;
    }
}
