<?php
require_once("./getset.php");
require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));


$id = $_GET['id'];

$getset = new GetSet();
$getset->value = $id;

$sql = "SELECT val FROM Tests WHERE id=".$getset->value;

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>