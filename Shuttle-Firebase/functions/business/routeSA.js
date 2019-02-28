const ERROR = require("../errors");
const routeDao = require("../dataAccess/routeDAO");
const originDao = require("../dataAccess/originDAO");
const personDao = require("../dataAccess/personDAO");

function createRoute(route){
   return checkRequirements(route)
        .then(()=>originDao.getOriginById(route.origin))
        .then((origin)=>{
            if(origin == null) throw ERROR.originDoesntExists;
            else return personDao.getUserById(route.driver);
        })
        .then((driver)=>{
            route.max=Number(route.max);
            route.passengersNumber = 0;
            if(driver == null) throw ERROR.userDoesntExists;
            else if(driver.type != "driver") throw ERROR.noPermissions;
            else return routeDao.insertRoute(route);
        } )

}

function getRouteById(id){
    let routeFin;

    return routeDao.getRouteById(id)
    .then((route)=>{
        if(route == null) throw ERROR.routeDoesntExists;
        else{
            routeFin = route;
            return personDao.getUserById(route.driver);
        }
    })
    .then((driver)=>{
        routeFin.driverName = driver.name;
        routeFin.driverSurname = driver.surname;
        routeFin.driverNumber = driver.number;
        routeFin.driverEmail = driver.email;
        return originDao.getOriginById(routeFin.origin);  
    })
    .then((origin)=>{
        routeFin.origin = origin.name;
        return routeFin;
    })
}

function searchRoutes(origin,destination){
    destination = Number(destination);
    return routeDao.getRoutesByOriginAndDestination(origin,destination);
}

function addToRoute(user,route,address){
    let passenger;
    return personDao.getUser(user.email)
    .then((result)=>{
        if (!result) throw ERROR.userDoesntExists;
        else{
            passenger = result;
            return routeDao.getRouteById(route.id);
        }
    })
    .then((route)=>{
        if (!route) throw ERROR.routeDoesntExists;
        else  if (route.passengers.length >= route.max) throw ERROR.routeSoldOut;
        else if (route.passengers.indexOf(passenger.id) != -1) throw ERROR.userAlreadyAdded;
        else return routeDao.addToRoute(passenger.id,route,address);
    })
}


function checkRequirements(route){
    return new Promise((resolve,reject)=>{
        if(route == null || route.driver == null || route.origin == null || route.destination == null ||
           route.max == null || Number(route.max) == NaN || Number(route.max)<0) reject(ERROR.badRequestForm);
        else resolve();
    });
}
module.exports = {
    createRoute:createRoute,
    getRouteById:getRouteById,
    searchRoutes:searchRoutes,
    addToRoute:addToRoute
}