<?php

$id = $_GET['id'];

ob_start();
include( 'template.php' ); 
$data = ob_get_contents();
@ob_end_clean();

print($data);

?>
