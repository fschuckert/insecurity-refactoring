<?php

function cleanValue($value, $db)
{
    if (function_exists('addslashes')) {
        echo "using quote";
        return $db->quote($value);

    } else {
        return addslashes($value);
    }
}

?>