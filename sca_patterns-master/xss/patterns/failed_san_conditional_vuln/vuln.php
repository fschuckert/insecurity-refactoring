<?php

require_once('./source.php');


$tainted = GETPOST('tainted');

print '<a href="'.$tainted . '"</a>';

?>