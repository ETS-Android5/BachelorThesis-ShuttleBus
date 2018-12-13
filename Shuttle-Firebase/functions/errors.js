/**
 * @description Errors which will be sent to the clients
 */
const errors = {

    /* General */
    server:{error:"server"},                                //Internal error.
    badRequestForm:{error:"badRequestForm"},                //The form requirements are not met.
    necessaryDataIsNull:{error:"necessaryDataIsNull"},      //The data that server needs is null. (Check index.html).

    /* SignIn */
    incorrectSignin:{error:"incorrectSignin"},              //The credentials are wrong.
    userDoesntExists:{error:"userDoesntExists"},            //The user you are trying to use doesn't exists.
    
    /* SignUp */
    noPermissions:{error:"noPermissions"},                  //The account you are using is not allowed to do this.
    userAlreadyExists:{error:"userAlreadyExists"},          //The user you are trying to register already exists in the db. (Checks the email)

    /* Origins */
    originDoesntExists:{error:"originDoesntExists"},        //The origin you are trying to get doesnt exists.
    originAlreadyExists:{error:"originAlreadyExists"}       //The origin you are trying to register already exists in the db. (Checks the name)
}

module.exports = errors;