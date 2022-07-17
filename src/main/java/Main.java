import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;



public class Main {
    public static void main(String[] args) {
        // ЗАДАНИЕ 1
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";

        createFileCSV();

        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);

        writeString(json, "data.json");

        // ЗАДАНИЕ 2
        createXML(list);
        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        writeString(json2, "data2.json");

        // ЗАДАНИЕ 3
        String json3 = readString("data.json");
        List<Employee> list3 = jsonToList(json3);
        list3.forEach(System.out::println);
    }

    private static String readString(String nameFile) {
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = (JSONArray) parser.parse(new FileReader(nameFile));
            return jsonObject.toJSONString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeString(String jsonString, String namefile) {
        try (FileWriter writer = new FileWriter(namefile)) {
            writer.write(jsonString);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Employee> parseCSV(String[] column, String fileName) {
        List<Employee> staff;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(column);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            staff = csv.parse();
            return staff;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<Employee> parseXML(String nameFile) {
        List<Employee> list = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(nameFile));

            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node_ = nodeList.item(i);
                if (Node.ELEMENT_NODE == node_.getNodeType()) {
                    Element element = (Element) node_;
                    long id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = element.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());
                    list.add(new Employee(id, firstName, lastName, country, age));
                }
            }

            return list;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String listToJson(List list) {
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(list, listType);
    }

    private static List<Employee> jsonToList(String jsonText) {
        try {
            List<Employee> list = new ArrayList<>();
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(jsonText);

            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                Employee employee = gson.fromJson(String.valueOf(jsonObject), Employee.class);
                list.add(employee);
            }
            return list;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void createFileCSV() {
        String[] employee = {
                "1,John,Smith,USA,25",
                "2,Inav,Petrov,RU,23"
        };

        try (CSVWriter writer = new CSVWriter(new FileWriter("data.csv"))) {
            for (String stringEmployee : employee) {
                writer.writeNext(stringEmployee.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createXML(List<Employee> employeeList) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element staff = document.createElement( "staff");
            document.appendChild(staff);

            for(Employee empl : employeeList) {
                Element employee = document.createElement( "employee");

                Element employeeId = document.createElement( "id");
                employeeId.setTextContent(String.valueOf(empl.id));
                employee.appendChild(employeeId);

                Element employeeFirstName = document.createElement( "firstName");
                employeeFirstName.setTextContent(String.valueOf(empl.firstName));
                employee.appendChild(employeeFirstName);

                Element employeeLastName = document.createElement( "lastName");
                employeeLastName.setTextContent(String.valueOf(empl.lastName));
                employee.appendChild(employeeLastName);

                Element employeeCountry = document.createElement( "country");
                employeeCountry.setTextContent(String.valueOf(empl.country));
                employee.appendChild(employeeCountry);

                Element employeeAge = document.createElement( "age");
                employeeAge.setTextContent(String.valueOf(empl.age));
                employee.appendChild(employeeAge);

                staff.appendChild(employee);
            }

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult( new File("data.xml"));
            TransformerFactory transformerFactory = TransformerFactory. newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

    }
}
