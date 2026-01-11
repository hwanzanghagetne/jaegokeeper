<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
    <head>
        <title>JACHO FILE LOADER</title>

        <style>
            html {
                font-size: 62.5%;
            }
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
                font-size: 1.6rem;
            }
            body {
                width: 100vw;
                height: 100dvh;
                padding: 2rem;
            }
            p {
                font-size: 1.2rem;
            }
            #imgUpForm, #imgUpRes {
                display: flex;
                justify-content: center;
                align-items: center;
                flex-direction: column;
                gap: 1rem;
                padding: 2rem;
                border: 1px solid #ccc;
                margin-bottom: 1rem;
            }
            #imgUpRes img {
                max-width: 600px;
                max-height: 600px;
                aspect-ratio: auto;
                object-fit: cover;
            }
            button {
                padding: 0.2rem 1rem;
                background: #88f;
                border: 1px solid #aaf;
                border-radius: 0.4rem;
                color: #fff;
            }
            button:hover {
                background: #66f;
                transition: 0.2s;
                transition-timing-function: ease-in;
            }
        </style>
    </head>

    <body>
        <h1>Img UPLOAD</h1>
        <p>${view}</p> <br/>

        <form id="imgUpForm" action="${pageContext.request.contextPath}/img/upload.jacho" method="post" enctype="multipart/form-data">
            <input id="imgUpIpt" type="file" name="file" accept=".jpg, .jpeg, .png, .webp"/>
            <button id="imgUpBtn" type="submit">Upload</button>
        </form>

        <div id="imgUpRes">
            <c:if test="${not empty success}">
                <c:choose>
                    <c:when test="${success}">
                        업로드 성공! imageId = ${imageId}
                        <img src="${pageContext.request.contextPath}/img/find/${imageId}" />
                    </c:when>
                    <c:otherwise>
                        업로드 실패: ${message}
                    </c:otherwise>
                </c:choose>
            </c:if>
        </div>
    </body>
</html>
