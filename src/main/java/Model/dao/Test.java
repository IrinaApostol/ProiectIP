package Model.dao;

import Model.dao.storage.Database;
import Model.dao.storage.TestDataModel;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public  class Test {


    /*
       /**
        * Aceasta functie este folosita pentru a obtine testele unei anumite probleme cu un anumit id
        *
        * @param problemId Id-ul problemei dupa care se cauta in baza de date
        * @return ArrayList de TestDataModel contine toate testele problemei
        */
    public ArrayList<TestDataModel> getTests(int problemId) {
        String query = "select id, test_in as input, test_out as output from problem_test where id_problem = ?";
        ArrayList<TestDataModel> tests = new ArrayList<>();
        try {
            Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query);
            statement.setInt(1, problemId);

            try (ResultSet rs = statement.executeQuery();) {
                while(rs.next()) {
                    TestDataModel test = new TestDataModel(rs.getInt("id") ,
                            rs.getString("input"), rs.getString("output"));
                    tests.add(test);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tests;
    }

    public static int getTestPercentage(int testId) {
        try {
            Connection myConn = Database.getConnection();
            PreparedStatement statement = myConn.prepareStatement("select percentage as percent from problem_test WHERE id = ? ");
            statement.setInt(1, testId);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("percent");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // nu avem procent (nu exista testul)
    }

    /**
     * Aceasta functie este folosita pentru a adauga teste la o anumita problema
     * @param problemId Id-ul problemei la care se va dauga testul
     * @param obj este obiectul de tip JSON care contine testele input, output si procentajul
     * @return
     */
    public static void addTestToProblem(JSONObject obj, int problemId )
    {
        String test_in = (String) obj.get("test_in");
        String test_out = (String) obj.get("test_out");
        Double percentage = (Double)  obj.get("percentage");

        String query = "insert into problem_test (id_problem , test_in, test_out, percentage) values(?,?,?,?)";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {
            statement.setInt(1, problemId);
            statement.setString(2, test_in);
            statement.setString(3, test_out);
            statement.setDouble(4, percentage);

            statement.executeUpdate();

        }catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    /*
    public static HashMap<Integer, HashMap<String, String>> getProblemTests(int problemId) {
        String query = "select id, test_in as input, test_out as output from problem_test where id_problem = ?";
        HashMap<Integer, HashMap<String, String>> tests = new HashMap<>();
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query);) {
            statement.setInt(1, problemId);
            try (ResultSet rs = statement.executeQuery();) {

                
                while(rs.next()) {
                    HashMap<String, String> inputOutputValues = new HashMap<>();
                    inputOutputValues.put("input", rs.getString("input"));
                    inputOutputValues.put("output", rs.getString("output"));
                    tests.put(rs.getInt("id"), inputOutputValues);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tests;
    }
    */
}
