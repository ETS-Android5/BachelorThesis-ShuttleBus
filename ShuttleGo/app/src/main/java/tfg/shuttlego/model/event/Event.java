package tfg.shuttlego.model.event;

public enum Event {

    /* ACCOUNT */
    SIGNIN,
    SIGNUP,
    SIGNOUT,

    /* ORIGIN */
    GETORIGINS,
    GETORIGINBYID,
    GETORIGINBYNAME,
    CREATEORIGIN,
    MODIFYORIGIN,
    DELETEORIGIN,

    /* ROUTE */
    CREATEROUTE,
    SEARCHROUTE,
    ADDTOROUTE,
    GETROUTEBYID,
    GETROUTEPOINTS
}
