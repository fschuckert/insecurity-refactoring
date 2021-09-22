<?php

require_once('database.php');

$db = new Database();

// $db->create();

$id = $_GET['id'];
$param = $_GET['value'];

if($param != null && $id != null)
{
    $db->insert($id, $param);
    $output = 'Successfully inserted.';
}
else {
    $output = 'id and value are required.';
}

echo $output;

// phpinfo();

?>