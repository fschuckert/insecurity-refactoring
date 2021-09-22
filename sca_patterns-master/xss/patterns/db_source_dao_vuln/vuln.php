<?php

require_once('database.php');
require_once('dao.php');

$db = new Database();

$id = intval($_GET['id']);

$dao = new DAO();

$dao->setId($id);


$tainted = $db->get_record($dao->getSQL());

echo $tainted;

?>