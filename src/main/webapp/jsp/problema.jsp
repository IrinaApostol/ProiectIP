<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Problema</title>
    <link rel="stylesheet" href="resources/css/navbar.css">
    <link rel="stylesheet" href="resources/css/ListaProbleme.css">
    <link rel="stylesheet" href="test.css">
    <script src="codemirror/lib/codemirror.js"></script>
    <script src="codemirror/addon/edit/matchbrackets.js"></script>
    <script src="codemirror/addon/edit/closebrackets.js"></script>
    <link href="codemirror/lib/codemirror.css" rel="stylesheet">
    <link href="codemirror/theme/dracula.css" rel="stylesheet">
    <script src="codemirror/mode/clike/clike.js"></script>

</head>
<body>


<div class="navbar">
    <div class="dropdown">
      <button class="dropbtn" onclick="#"> PROBLEMEE
        <i class="fa fa-caret-down"></i>
      </button>
      <div class="dropdown-content">
        <a href="GetProblems?grade=9" >Clasa a IX-a</a>
        <a href="GetProblems?grade=10">Clasa a X-a</a>
        <a href="GetProblems?grade=11">Clasa a XI-a</a>
      </div>
    </div>
    <div class="dropdown">
     
     
  
    </div>
      <a href="html/adaugaIntrebare.html" id="logo">ADAUGA PROBLEMA</a>
      <a href="html/cont.html" id="logo">CONTUL MEU</a>
      <a href="./index.html" id="logo">DECONECTARE</a>
  </div>
<jsp:useBean id="problem" scope="request" type="org.json.JSONObject"/>

<form method = "POST" style=" padding-left: 200px ; padding-right: 200px">
    <br> <br>
        <h3 style="font-family: Ubuntu;" >Enunt: ${problem.getString("statement")} </h3><br>
    <br>
    <table class="problemStyle">
        <tr>
            <th>Numar Problema</th>
            <th>Nume Problema</th>
            <th>Dificultate</th>
            <th>Categorie</th>
        </tr>
        <tr>
            <td> ${problem.getInt("id")}</td>
            <td> ${problem.getString("title")}</td>
            <td> ${problem.getString("difficulty")}</td>
            <td>${problem.getInt("category")}</td>
        </tr>
    </table><br>
    <h3 style="font-family: Ubuntu;">Exemple: </h3><br>
    <table class="problemStyle">
        <tr>
            <th>Input</th>
            <th>Output</th>
        </tr>
        <tr>
            <td>${problem.getString("test_in")}</td>
            <td>${problem.getString("test_out")}</td>
        </tr>
    </table>
    <br> <br> 
    <h3>Scrie solutie mai jos:</h3><br>
    <textarea class="text" id="editor" name="solutionText">#include<iostream>
        using namespace std;
        int main ()
        {
            return 0;
        }
    </textarea>
    <script>
        var editor= CodeMirror.fromTextArea
        (document.getElementById('editor'),{
            mode: "clike",
            theme: "dracula",
            lineNumbers: true,
            autoCloseBrackets: true
        });
    </script>
    <br>
    <button formaction = "SendSolution" type="submit" class="register">Trimite
    </button>

</form>
</body>
</html>
