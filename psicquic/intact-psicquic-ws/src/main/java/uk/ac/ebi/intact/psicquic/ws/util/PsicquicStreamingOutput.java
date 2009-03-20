/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.psicquic.ws.util;

import org.hupo.psi.mi.psicquic.RequestInfo;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.PsicquicService;

import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.WebApplicationException;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * TODO write description of the class.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsicquicStreamingOutput implements StreamingOutput {

    private PsicquicService psicquicService;
    private String query;
    private QueryResponse response;

    public PsicquicStreamingOutput(PsicquicService psicquicService, String query) {
        this.psicquicService = psicquicService;
        this.query = query;
    }

    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setResultType("psi-mi/tab25");

        PrintWriter out = new PrintWriter(outputStream);

        int firstResult = 0;
        int maxResults = 50;

        do {
            reqInfo.setFirstResult(firstResult);
            reqInfo.setBlockSize(maxResults);

            System.out.println("Searching: " + query + " " + firstResult);

            try {
                response = psicquicService.getByQuery(query, reqInfo);

                out.write(response.getResultSet().getMitab());
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }

            out.flush();

            firstResult = firstResult + maxResults;

        } while (response.getResultSet().getMitab().length() > 0);

        out.close();
    }

    public QueryResponse getQueryResponse() {
        return response;
    }
}
