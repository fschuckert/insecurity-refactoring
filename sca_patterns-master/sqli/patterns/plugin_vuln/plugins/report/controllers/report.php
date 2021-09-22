<?php

require_once('./TemplateController.php');

class Report_Controller extends Template_Controller
{
    public function index()
    {
        echo "Hello";
    }

    public function edit($id)
    {
        echo "<h1>Hello</h1>";
        $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
        $sql = "SELECT val FROM Tests WHERE id=$id";

        $results = $db->query($sql);

        foreach ($results as $row) 
        {
            echo $row['val'];
        }
    }
}