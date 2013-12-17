<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults" %>
<%@ page import="java.util.List" %>
<html>

<%@include file="header.jsp"%>

<div id="local-masthead" class="masthead grid_24 nomenu">

    <!-- local-title -->

    <div class="grid_24 alpha" id="local-title">
        <h1><a href="${complex_home_url}" title="Back to ${complex_portal_name} homepage"><img src="http://www.ebi.ac.uk/intact/images/IntAct_logo.png"> Complex Portal</a></h1>
    </div>

    <!-- /local-title -->

    <!-- /local-nav -->

    <nav>
        <ul class="grid_24" id="local-nav">
            <li class="first active"><a href="">Home</a></li>
            <li><a href="${complex_documentation_url}">Documentation</a></li>
            <li class="last"><a href="${complex_about_url}">About ${complex_portal_name}</a></li>
            <!-- If you need to include functional (as opposed to purely navigational) links in your local menu,
                 add them here, and give them a class of "functional". Remember: you'll need a class of "last" for
                 whichever one will show up last...
                 For example: -->
        </ul>

    </nav>

    <!-- /local-nav -->

</div>

</header>

<div id="content" role="main" class="grid_24 clearfix">
    <!-- Suggested layout containers -->
    <!--
    <section>
        <h2>Fluid Width</h2>

        <div class="grid_12 alpha">
            <h3>Code</h3>
            <ul>
                <li><a href="ebi-level2-boilerplate-blank-fluid.txt">ebi-level2-boilerplate-blank-fluid.txt</a></li>
            </ul>
        </div>

        <div class="grid_12 omega">
            <h3>Examples</h3>
            <ul>
                <li><a href="ebi-level2-boilerplate-blank-fluid.html">Blank</a> - big, empty space between header and footer</li>
                <li><a href="ebi-level2-boilerplate-right-sidebar-fluid.html">Right sidebar</a> - for content-driven pages deeper in your site's hierarchy, the sidebar could contain secondary navigation</li>
                <li><a href="ebi-level2-boilerplate-4cols-fluid.html">Four columns</a> - an example of dividing the page into four broad content areas</li>
                <li><a href="ebi-level2-boilerplate-search-results-fluid.html">Search results</a> - an example page with filters or facets for your results in a left-hand sidebar, and a right-hand sidebar for global results</li>
            </ul>
            <h3>Search Results Examples</h3>
            <ul>
                <li><a href="ebi-search-results-complete.html">Search Results Complete</a> - template for a basic search results page </li>
                <li><a href="ebi-no-search-results.html">No Search Results</a> - template for a page of zero results </li>
            </ul>
        </div>

    </section>
    -->
    <!-- End suggested layout containers -->

    <jsp:useBean id="details" class="uk.ac.ebi.intact.service.complex.view.ComplexDetails" scope="session"/>
    <h2 class="titleDetails"><%if(details.getName() != null){%><%= details.getName() %><%}else{%>&lt;Not available&gt;<%}%> <%if (details.getSpecie() != null){%>(<%= details.getSpecie() %>)<%}%></h2>
    <h3 class="subtitleDetails">Intact AC: <%if(details.getAc() != null){%><%= details.getAc() %><%}else{%>&lt;Not available&gt;<%}%></h3>
    <div class="details">
        <div class="section">
            <h4 class="sectionTitle">Summary</h4>
            <label class="sectionEntry">Systematic Name:</label>
            <br>
            <%if(details.getSystematicName() != null){%>
                <label class="sectionValue"><%=details.getSystematicName()%></label>
            <%}else{%>&lt;Not available&gt;<%}%>
            <br><br>
            <label class="sectionEntry">Synonyms:</label>
            <br>
            <%if(details.getSynonyms() != null){%>
                <%for( String synonym : details.getSynonyms() ) {%>
                    <label class="sectionValue"><%=synonym%><br></label>
                <%}%>
            <%}else{%>&lt;Not available&gt;<%}%>
            <br><br>
            <label class="sectionEntry">Function:</label>
            <br>
            <%if(details.getFunction() != null){%>
                <label class="sectionValue"><%=details.getFunction()%></label>
            <%}else{%>&lt;Not available&gt;<%}%>
            <br><br>
            <label class="sectionEntry">Properties:</label>
            <br>
            <%if(details.getProperties() != null){%>
                <label class="sectionValue"><%=details.getProperties()%></label>
            <%}else{%>&lt;Not available&gt;<%}%>
        </div>
        <br>
        <div class="section">
            <h4 class="sectionTitle">Participants</h4>
            <table class="participants">
                <thead>
                    <tr>
                        <td>ID</td>
                        <td>Name</td>
                    </tr>
                </thead>
                <tbody>
                    <%  List<String> names = details.getComponentsName();
                        List<String> acs = details.getComponentsAC();
                        for(int i = 0; i < acs.size(); ++i){%>
                        <tr>
                            <td><a href="<%=request.getContextPath()%>/details/<%=acs.get(i)%>"><%=acs.get(i)%></a></td>
                            <td><%=names.get(i)%></td>
                        </tr>
                    <%}%>
                </tbody>
            </table>
        </div>
    </div>
</div>


<%@include file="footer.jsp"%>
</html>