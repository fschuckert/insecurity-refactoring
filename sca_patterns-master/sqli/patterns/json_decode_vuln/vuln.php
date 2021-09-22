<?php
require_once("./decoder.php");
require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = $_GET['id'];
$decoded = Decoder::decode($id);
$sql = "SELECT val FROM Tests WHERE id=$decoded";

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>