<?php
require_once("./application.php");
require_once("./init.php");

$app = new CApplication();

$app->setRequest($_REQUEST);


$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = $app->request['id'];

$sql = "SELECT val FROM Tests WHERE id=$id";

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>