/**
 * (C) Copyright IBM Corporation 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openliberty.tools.common.plugins.config;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;

public class MbeanConfigXmlDocument extends XmlDocument {
    public static final String MBEAN_XML_FILENAME = "mbean-config.xml";

    public MbeanConfigXmlDocument() {
        try {
            createDocument("server");
        } catch (ParserConfigurationException e) {
            // it should never occur
            e.printStackTrace();
        }
    }

    public void createApplicationMonitorElement() {
        Element varElement = doc.createElement("variable");
        varElement.setAttribute("name", "io.openliberty.tools.update.trigger");
        varElement.setAttribute("defaultValue", "polled");
        doc.getDocumentElement().appendChild(varElement);

        Element appMonitor = doc.createElement("applicationMonitor");
        appMonitor.setAttribute("updateTrigger", "${io.openliberty.tools.update.trigger}");
        doc.getDocumentElement().appendChild(appMonitor);
    }

    public void writeMbeanConfigXmlDocument(File serverDirectory) throws IOException, TransformerException {
        File mbeanXml = getMbeanConfigXmlFile(serverDirectory);
        if (!mbeanXml.getParentFile().exists()) {
            mbeanXml.getParentFile().mkdirs();
        }
        writeXMLDocument(mbeanXml);
    }
    
    public static File getMbeanConfigXmlFile(File serverDirectory) {
        File f = new File(serverDirectory, "configDropins/defaults/" + MBEAN_XML_FILENAME); 
        return f;
    }
}
