package com.wuzhi.server.common.context

class UserContext{
    companion object{
        private val userId: ThreadLocal<String> = ThreadLocal();
        fun setUserId(id: String){
            userId.set(id)
        }
        fun getUserId(): String{
            return userId.get()
        }
        fun removeUserId(){
            userId.remove()
        }
    }
}