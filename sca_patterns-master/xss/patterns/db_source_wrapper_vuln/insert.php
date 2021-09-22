<?php

require_once('database.php');

$db = new Database();

// $db->create();

$name = $_GET['table_name'];

if($name != null)
{
    $db->insert($name);
    $output = 'Successfully inserted.';
}
else {
    $output = 'table name is required.';
}

echo $output;

// phpinfo();

?>