<?php

/**
 *  Return value of a param into GET or POST supervariable
 *  @param          paramname   Name of parameter to found
 *  @param			check		Type of check (''=no check,  'int'=check it's numeric, 'alpha'=check it's alpha only)
 *  @param			method		Type of method (0 = get then post, 1 = only get, 2 = only post, 3 = post then get)
 *  @return         string      Value found or '' if check fails
 */
function GETPOST($paramname, $check='', $method=0)
{
    if (empty($method)) $out = isset($_GET[$paramname])?$_GET[$paramname]:(isset($_POST[$paramname])?$_POST[$paramname]:'');
    elseif ($method==1) $out = isset($_GET[$paramname])?$_GET[$paramname]:'';
    elseif ($method==2) $out = isset($_POST[$paramname])?$_POST[$paramname]:'';
    elseif ($method==3) $out = isset($_POST[$paramname])?$_POST[$paramname]:(isset($_GET[$paramname])?$_GET[$paramname]:'');

    if (!empty($check))
    {
        // Check if numeric
        if ($check == 'int' && ! preg_match('/^[\.,0-9]+$/i',trim($out))) $out='';
        // Check if alpha
        //if ($check == 'alpha' && ! preg_match('/^[ =:@#\/\\\(\)\-\._a-z0-9]+$/i',trim($out))) $out='';
        if ($check == 'alpha' && preg_match('/"/',trim($out))) $out='';    // Only " is dangerous because param in url can close the href= or src= and add javascript functions
    }

    return $out;
}


?>