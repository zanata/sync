<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html class="login-pf">
<head lang="en">
    <title>Login Page | Zanata Sync</title>
    <meta charset="UTF-8">
    <meta name='viewport' content='width=device-width,initial-scale=1'>
    <c:url var="cssBundle" value="/app/bundle.css"/>
    <link type="text/css" rel="stylesheet" href="${cssBundle}">
</head>
<body>

<c:set var="message" value='${param.error ? "Log in failed" : ""}'/>

<div class="container">
    <div class="row">
        <span class="bg-danger">${message}</span>
    </div>
    <div class="row">
        <div class="col-sm-7 col-md-6 col-lg-5 login">
            <form class="form-horizontal" role="form" method=post action="j_security_check">
                <div class="form-group">
                    <label for="j_username"
                            class="col-sm-2 col-md-2 control-label">Username</label>
                    <div class="col-sm-10 col-md-10">
                        <input type="text" class="form-control" id="j_username"
                                name="j_username" placeholder="username"
                                tabindex="1">
                    </div>
                </div>
                <div class="form-group">
                    <label for="j_password"
                            class="col-sm-2 col-md-2 control-label">Password</label>
                    <div class="col-sm-10 col-md-10">
                        <input type="password" class="form-control"
                                id="j_password" name="j_password" placeholder=""
                                tabindex="2">
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-xs-4 col-sm-4 col-md-4 submit">
                        <button type="submit" class="btn btn-primary btn-lg"
                                tabindex="3">Log In
                        </button>
                    </div>
                </div>
            </form>
        </div>
        <div class="col-sm-5 col-md-6 col-lg-7 details">
            <p>
                <strong>Welcome to Zanata Sync</strong>
                If you want to have an account, please contact Zanata team.
            </p>
        </div>
    </div>
</div>

</body>
</html>
