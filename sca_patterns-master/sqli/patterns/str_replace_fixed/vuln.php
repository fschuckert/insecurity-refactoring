<?php
require_once("./sanitize.php");
require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = sanitize($_GET['id']);

$sql = "SELECT val FROM Tests WHERE val='$id'";

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>