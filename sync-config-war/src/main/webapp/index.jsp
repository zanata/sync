<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <meta name='viewport' content='width=device-width,initial-scale=1'>
    <c:url var="cssBundle" value="/bundle.css"/>
    <c:url var="jsBundle" value="/frontend.bundle.min.js"/>
    <link type="text/css" rel="stylesheet" href="${cssBundle}">
    <title>Zanata Sync</title>
</head>

<body class="cards-pf">
<main role="main">
    <div id="main-content"
            data-api-url=''
            data-app-basename='${pageContext.request.contextPath}'
            data-user='${requestScope.user}'
            data-zanata-oauth-urls='${requestScope.zanataOAuthUrls}'
            data-src-repo-plugins='${requestScope.srcRepoPlugins}'
            data='{ "dev": "false"}'
    >
    </div>

</main>

<script src="//cdnjs.cloudflare.com/ajax/libs/modernizr/2.8.3/modernizr.min.js"></script>
<script src="${jsBundle}"></script>
</body>
</html>

