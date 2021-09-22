<?php

require_once('databaseImpl.php');
$db = new DatabaseImpl();

$id = intval($_GET['id']);

$sql = "SELECT * FROM user WHERE id=:id";
$params = array(
    ':id' => $id
);

$tainted = $db->get_records_sql($sql, $params);

echo $tainted;

?>