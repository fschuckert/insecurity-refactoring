<?php

require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
if(!is_numeric($id)){header('Location: '.url('/'));}

$id = $_GET['id'];

$sql = "SELECT val FROM Tests WHERE id=$id";
foreach ($db->query($sql) as $row) 
{
    echo $row['val'];
}


?>