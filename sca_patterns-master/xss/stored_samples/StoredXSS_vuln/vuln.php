<?php

require_once('database.php');

$db = new Database();

// $db->create();

$id = $_GET['id'];

if($id != null)
{
    $output = $db->getValue($id);
}
else {
    $output = 'ID is required.';
}

echo $output;

?>