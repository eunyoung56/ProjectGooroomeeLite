package kr.co.gooroomeelite.views.login
/**
 * @author Gnoss
 * @email silmxmail@naver.com
 * @created 2021-06-21
 * @desc
 */
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kr.co.gooroomeelite.R
import kr.co.gooroomeelite.databinding.ActivityLoginnicknameBinding

class LoginNicknameActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginnicknameBinding
    var bundle : Bundle? = null
    var email : String? = null
    var password : String? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null
    var imm : InputMethodManager? = null
    private val maxlength = 6
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginnicknameBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        bundle = intent.getBundleExtra("bundle")
        email = bundle?.getString("email")
        password= bundle?.getString("password")
        imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        setContentView(binding.root)


        //백버튼 활성화
        binding.icBack.setOnClickListener {
            if (password!=null){
                startActivity(Intent(this,LoginFirstActivity::class.java))
            }
            else{
                startActivity(Intent(this,LoginActivity::class.java))
            }
        }

        binding.editTextNickname.setOnFocusChangeListener { v, hasFocus ->
            when (hasFocus) {
                true -> changecolor()
                false -> changecolor()
            }
        }

        changecolor()



        binding.editTextNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.editTextNickname.setBackgroundResource(R.drawable.btn_skyblue)
                binding.editTextNickname.apply {
                    if (this.isFocusable && s.toString() != "") {
                        val string: String = s.toString()
                        val len = string.length
                        if (len > maxlength) {
                            this.setText(string.substring(0, maxlength))
                            this.setSelection(maxlength)
                        } else {
                            binding.textCount.text = "$len / $maxlength"
                        }
                    } else {
                        binding.textCount.text = "0 / $maxlength"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.btnLoginNext.setOnClickListener {
            if (binding.editTextNickname.text.toString().length>6) {
                binding.tvError.text = "닉네임은 최대 6글자입니다."
                binding.editTextNickname.setBackgroundResource(R.drawable.btn_red)
            } else {
                binding.tvError.text = ""
                binding.editTextNickname.setBackgroundResource(R.drawable.btn_white)
                moveNextPage()
            }
        }
    }

    private fun moveNextPage(){
            val intent = Intent(this, LoginStudyTimeActivity::class.java)
            val bundle = Bundle()
        if (password!=null){
            bundle.putString("password",password)
            bundle.putString("email",email)
            bundle.putString("nickname",binding.editTextNickname.text.toString())
            intent.putExtra("bundle",bundle)
            startActivity(intent)
            finish()
        }
        else{
            bundle.putString("email",email)
            bundle.putString("nickname",binding.editTextNickname.text.toString())
            intent.putExtra("bundle",bundle)
            startActivity(intent)
            finish()

        }

        }

    private fun changecolor() {
        binding.editTextNickname.setOnFocusChangeListener { v, hasFocus ->
            when (hasFocus) {
                true -> v.setBackgroundResource(R.drawable.btn_skyblue)
                false -> v.setBackgroundResource(R.drawable.btn_white)
            }
        }
    }
    fun hideKeyboard(v: View){
        if (v!=null){
            imm?.hideSoftInputFromWindow(v.windowToken,0)
        }
    }
}