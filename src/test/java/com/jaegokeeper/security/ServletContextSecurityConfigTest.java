package com.jaegokeeper.security;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class ServletContextSecurityConfigTest {

    private static final String AUTHENTICATION_PRINCIPAL_RESOLVER =
            "org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver";

    @Test
    public void 실제_MVC_XML에_AuthenticationPrincipal_처리기가_등록되어있다() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        Document document = factory.newDocumentBuilder().parse(
                Path.of("src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml").toFile()
        );

        NodeList beans = document.getElementsByTagNameNS(
                "http://www.springframework.org/schema/beans",
                "bean"
        );

        int resolverCount = 0;
        for (int i = 0; i < beans.getLength(); i++) {
            if (AUTHENTICATION_PRINCIPAL_RESOLVER.equals(
                    beans.item(i).getAttributes().getNamedItem("class").getNodeValue())) {
                resolverCount++;
            }
        }

        assertEquals(1, resolverCount);
    }
}
