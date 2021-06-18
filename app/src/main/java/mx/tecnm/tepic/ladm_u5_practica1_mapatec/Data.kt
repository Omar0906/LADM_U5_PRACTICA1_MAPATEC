package mx.tecnm.tepic.ladm_u5_practica1_mapatec
import com.google.firebase.firestore.GeoPoint
class Data {
    var nombre :String=""
    var posicion1 : GeoPoint = GeoPoint(0.0,0.0)
    var posicion2 : GeoPoint = GeoPoint(0.0,0.0)
    var interno : ArrayList<String> = ArrayList<String>()
    constructor(nombre:String,posicion1:GeoPoint,posicion2:GeoPoint,interno:ArrayList<String> = ArrayList<String>()){
        this.nombre = nombre
        this.posicion1 = posicion1
        this.posicion2 = posicion2
        this.interno = interno
    }
    override fun toString(): String {
        return nombre+"\n${posicion1.latitude},${posicion1.longitude}\n${posicion2.latitude},${posicion2.longitude}"
    }
    fun estoyEn(posicionActual:GeoPoint):Boolean{
        if(posicionActual.latitude >= posicion1.latitude && posicionActual.latitude<=posicion2.latitude){
            if(invertir(posicionActual.longitude) >= invertir(posicion1.longitude) && (invertir(posicionActual.longitude)<=invertir(posicion2.longitude))){
                return true
            }
        }
        return false
    }
    fun puntoMedio():GeoPoint{
        var medioLatitud:Double
        var medioLongitud:Double
        medioLatitud = (this.posicion2.latitude - this.posicion1.latitude)/2
        medioLongitud = (this.posicion2.longitude - this.posicion1.longitude)/2
        return GeoPoint(this.posicion1.latitude+medioLatitud,this.posicion1.longitude+medioLongitud)
    }
    private fun invertir(valor:Double):Double{
        return valor*-1
    }
}