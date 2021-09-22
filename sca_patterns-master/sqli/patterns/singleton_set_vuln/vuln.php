<?php
require_once("./application.php");
require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = $_GET['id'];
$sql = "SELECT val FROM Tests WHERE id=" . $id;
Application::setApplication($sql);

$results = $db->query(Application::app());

foreach ($results as $row) 
{
    echo $row['val'];
}


?>