<%-- 
    Document   : index
    Created on : Oct 16, 2013, 4:21:07 PM
    Author     : jbf
--%>

<%@page import="org.das2.das2server.Config"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Das2Server</title>
    </head>
    <body>
        <h1>Introduction</h1>
        <p>This here web application implements a das2server with a Java Web application.  The instance is here:
            <a href="das2server">das2server</a>.  It is configured by setting directories in 
            <%= Config.resolveProperty( Config.PROP_HOME ) %>.
    </body>
</html>
