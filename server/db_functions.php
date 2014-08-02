<?php
class DB_Functions {
 
    private $db;
 
    //put your code here
    // constructor
    function __construct() {
        include_once './db_connect.php';
        // connecting to database
        $this->db = new DB_Connect();
        $this->db->connect();
    }
 
    // destructor
    function __destruct() {
         
    }
 
    /**
     */
    public function removeUser($gcm_regid) {
        // delete user from database
        $result = mysql_query("DELETE from gcm_users where gcm_regid='$gcm_regid'");

        // check for successful store
        if ($result) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Storing new user
     * returns user details
     */
    public function storeUser($name, $email, $gcm_regid) {
        // insert user into database
        $result = mysql_query("INSERT INTO gcm_users(name, email, gcm_regid, created_at) VALUES('$name', '$email', '$gcm_regid', NOW())");
        // check for successful store
        if ($result) {
            // get user details
            $id = mysql_insert_id(); // last inserted id
            $result = mysql_query("SELECT * FROM gcm_users WHERE id = $id") or die(mysql_error());
            // return user details
            if (mysql_num_rows($result) > 0) {
                return mysql_fetch_array($result);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
 
    /**
     * Getting users ID, indexed by 1000 users
     */
    public function getUsers($index) {
	$start = $index * 1000;
	$end = $index * 1000 + 999;
        $result = mysql_query("SELECT gcm_regid FROM gcm_users limit $start, $end");

	$out = array();
	while ($row = mysql_fetch_array($result)) {
    		$out[] = $row[0];
	}

        return $out;
    }
 
}
 
?>
