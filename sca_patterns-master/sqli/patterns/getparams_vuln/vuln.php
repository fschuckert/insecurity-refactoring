<?php
require_once('./source.php');
require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = Params::getParam('id');

$sql = "SELECT val FROM Tests WHERE id=$id";

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>