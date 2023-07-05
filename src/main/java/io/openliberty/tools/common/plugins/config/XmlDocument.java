/**
 * (C) Copyright IBM Corporation 2017, 2021.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public abstract class XmlDocument {
    
    protected Document doc;
    
    public void createDocument(String rootElement) throws ParserConfigurationException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);
        Element element = doc.createElement(rootElement);
        doc.appendChild(element);
    }
    
    public void createDocument(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setCoalescing(true);
        builderFactory.setIgnoringElementContentWhitespace(true);
        builderFactory.setValidating(false);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        doc = builder.parse(xmlFile);
    }

    public void createComment(String comment) {
        createComment(findServerElement(), comment);
    }
    
    // add comment to the end of the children
    public void createComment(Element elem, String comment) {
        Comment commentElement = doc.createComment(comment);
        appendBeforeBlanks(elem, commentElement);
    }

    private void appendBeforeBlanks(Element elem, Node childElement) {
        Node lastchild = elem.getLastChild();
        if (isWhitespace(lastchild)) {
            // last child is the whitespace preceding the </element> so insert before that
            elem.insertBefore(childElement, lastchild);
        } else {
            elem.appendChild(childElement);
        }
    }

    public Element findServerElement() {
        return doc.getDocumentElement(); // defined for this type of file
    }

    public void writeXMLDocument(String fileName) throws IOException, TransformerException {
        File f = new File(fileName);
        writeXMLDocument(f);
    }
    
    public void writeXMLDocument(File f) throws IOException, TransformerException {
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        FileOutputStream outFile = new FileOutputStream(f);
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outFile);
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
        if (isIndented()) {
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
        } else {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        }
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        transformer.transform(source, result);
        outFile.close();
    }

    protected boolean isIndented() {
        // if the first child is just white space then the document contains indentation information
        Node x = doc.getDocumentElement().getFirstChild();
        return isWhitespace(x);
    }

    protected boolean isWhitespace(Node node) {
        return node != null && node instanceof Text && ((Text)node).getData().trim().isEmpty();
    }

    public static void addNewlineBeforeFirstElement(File f) throws IOException {
        // look for "<?xml version="1.0" ... ?><server .../>" and add a newline
        byte[] contents = Files.readAllBytes(f.toPath());
        String xmlContents = new String(contents, StandardCharsets.UTF_8);
        xmlContents = xmlContents.replace("?><", "?>"+System.getProperty("line.separator")+"<");
        Files.write(f.toPath(), xmlContents.getBytes());
    }
}
