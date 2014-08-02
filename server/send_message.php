<?php
if (isset($_POST["message"]) && isset($_POST["uuid"])) {

    $uuid = $_POST["uuid"];
    if(0 != strcmp($uuid, "75ea8094-b8e7-11e3-bac1-a33dc12f4608")) {
        die;
    }

    $message = $_POST["message"];
     
    include_once './GCM.php';

    include_once './db_functions.php';

    $db = new DB_Functions();

    $gcm = new GCM();
    
    $res = $db->getUsers(0);
 
    $registatoin_ids = $res;
    $messages = array("message" => $message);

    $result = $gcm->send_notification($registatoin_ids, $messages);
 
    echo $result;
}
?>
