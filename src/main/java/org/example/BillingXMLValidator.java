package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

public class BillingXMLValidator {
    public static void main(String[] args) {
        String xmlFilePath = "C:\\Users\\User\\IdeaProjects\\untitled\\src\\main\\java\\org\\example\\billing.xml";
        String xsdFilePath = "C:\\Users\\User\\IdeaProjects\\untitled\\src\\main\\java\\org\\example\\billing.xsd";
        double minBalance = 120.0;
        int minId = 2;
        int idToAdd = 3;
        String nameToAdd = "Николай Николаев";
        double balanceToAdd = 200.0;
        int idToRemove = 2;


        BillingXMLValidator.validateXML(xmlFilePath, xsdFilePath);


        BillingXMLValidator.searchWithSAX(xmlFilePath, minBalance, minId);


        BillingXMLValidator.addToXML(xmlFilePath, idToAdd, nameToAdd, balanceToAdd);


        BillingXMLValidator.removeFromXML(xmlFilePath, idToRemove);
    }

    private static void validateXML(String xmlFilePath, String xsdFilePath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdFilePath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlFilePath)));
            System.out.println("XML is valid.");
        } catch (Exception e) {
            System.out.println("XML is NOT valid:");
            System.out.println(e.getMessage());
        }
    }
    private static void searchWithSAX(String xmlFilePath, double minBalance, int minId) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                boolean foundSubscriber = false;

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("subscriber")) {
                        String balanceValue = attributes.getValue("balance");
                        String idValue = attributes.getValue("id");
                        if (balanceValue != null && idValue != null) {
                            double balance = Double.parseDouble(balanceValue.trim());
                            int id = Integer.parseInt(idValue.trim());
                            if (balance >= minBalance && id > minId) {
                                foundSubscriber = true;
                                System.out.println("Subscriber with balance >= " + minBalance + " and id > " + minId + ":");
                            }
                        }
                    }
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("subscriber")) {
                        foundSubscriber = false;
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    if (foundSubscriber) {
                        System.out.println(new String(ch, start, length));
                    }
                }
            };

            saxParser.parse(xmlFilePath, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
    private static void addToXML(String xmlFilePath, int idToAdd, String nameToAdd, double balanceToAdd) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFilePath);

            Element newSubscriber = doc.createElement("subscriber");
            newSubscriber.setAttribute("id", Integer.toString(idToAdd));

            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(nameToAdd));
            newSubscriber.appendChild(name);

            Element balance = doc.createElement("balance");
            balance.appendChild(doc.createTextNode(Double.toString(balanceToAdd)));
            newSubscriber.appendChild(balance);

            Node billing = doc.getElementsByTagName("billing").item(0);
            billing.appendChild(newSubscriber);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(xmlFilePath));
            transformer.transform(source, result);
            System.out.println("Added new subscriber to XML.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeFromXML(String xmlFilePath, int idToRemove) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFilePath);

            NodeList subscribers = doc.getElementsByTagName("subscriber");
            for (int i = 0; i < subscribers.getLength(); i++) {
                Node subscriber = subscribers.item(i);
                if (subscriber.getNodeType() == Node.ELEMENT_NODE) {
                    Element subscriberElement = (Element) subscriber;
                    int id = Integer.parseInt(subscriberElement.getAttribute("id"));
                    if (id == idToRemove) {
                        subscriber.getParentNode().removeChild(subscriber);
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(new File(xmlFilePath));
                        transformer.transform(source, result);
                        System.out.println("Removed subscriber with id " + idToRemove + " from XML.");
                        return;
                    }
                }
            }
            System.out.println("Subscriber with id " + idToRemove + " not found in XML.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
