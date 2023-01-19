package dbHandling;
import org.json.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.rmi.MarshalledObject;
import java.sql.*;

import javax.xml.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DBHandler {
    // A database handler for the Salex-Inn Database
    // Provides methods to connect to the DB, retrieve and make reservations,
    // and provide other useful tools for the Website Application for the Salex-Inn

    private  void displayResults(ResultSet resultSet, String table)
    {
        // A helper method to display results of a resultSet.
        // right now its just for the Reservation table
        switch (table){
            case "reservation":
                try{
                    System.out.println("ID\tDate\t\tRoomID\tGuestID");
                    // System.out.println("--------------------------------");
                    while (resultSet.next()){
                        System.out.println(resultSet.getInt("ID") +"\t"+ resultSet.getString("Date")
                        + "\t" + resultSet.getInt("RoomId") + "\t\t" + resultSet.getInt("GuestID"));
                    }
                }
                catch (SQLException e){e.printStackTrace();}
                break;
            default:
                break;
        }
    }
    private  String toString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }
    private  Document getReservations(){
        try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root","MySQLPass");

        ){
            Statement stmt = conn.createStatement();
            ResultSet rs;
            // stmt.execute("insert into salexinndb.guest values (2, 'justAEmail@aol.com', '1234567890', 'Mario Dopico');");
            rs = stmt.executeQuery("select * from salexinndb.reservation;");
            // displayResults(rs, "reservation");
            return toDocument(rs);
        } catch (SQLException | ParserConfigurationException throwables) {
            //System.out.println("boo");
            throwables.printStackTrace();
        }
        // System.out.println("Something went  wrong");

        return null;
    }
    private  Document toDocument(ResultSet rs)
            throws ParserConfigurationException, SQLException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder        = factory.newDocumentBuilder();
        Document doc                   = builder.newDocument();

        Element results = doc.createElement("Results");
        doc.appendChild(results);

        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount           = rsmd.getColumnCount();

        while (rs.next())
        {
            // System.out.println("enter");
            Element row = doc.createElement("Row");
            results.appendChild(row);

            for (int i = 1; i <= colCount; i++)
            {
                String columnName = rsmd.getColumnName(i);
                Object value      = rs.getObject(i);

                Element node      = doc.createElement(columnName);
                node.appendChild(doc.createTextNode(value.toString()));
                row.appendChild(node);
            }
        }
        return doc;
    }
    public  String getReservationsXMLString(){
        return toString(getReservations());
    }
    public  String getReservationsJSON(){
        try {
            JSONObject json = XML.toJSONObject(getReservationsXMLString());
            String jsonString = json.toString(4);
            // System.out.println(jsonString);
            return jsonString;

        }catch (JSONException e) {
            System.out.println(e.toString());
        }
        return "";
    }
    public void getReservationsWhen(String month, String year){
        // this method will get all reservations during a given month and year
        // it generates an SQL query that returns all reservations that match that time...
        // it SHOULD also validate whether the input month and year are actually such and are not attempting to inject
        // something not allowed or insecure.
    }


    public static void main(String[] args) {

        DBHandler dbh = new DBHandler();
        System.out.println(dbh.getReservationsXMLString());
        System.out.println(dbh.getReservationsJSON());

        System.out.println("yay!");
    }
}
