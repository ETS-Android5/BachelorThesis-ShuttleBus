
const personDAO = require("../dataAccess/personDAO");


function signIn(email, password) {

    return personDAO
    .getUser(email)
    .then(user => {
        if (user && user.password == password) return user;
        else return null;
    });
};//signIn


module.exports = {

    signIn:signIn
}
