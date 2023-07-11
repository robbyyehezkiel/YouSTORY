package bangkit.robbyyehezkiel.androidintermediate.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.UserPreferences
import bangkit.robbyyehezkiel.androidintermediate.utils.dataStore
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.AuthenticateViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.SettingViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.ViewModelSettingFactory
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.databinding.FragmentLoginBinding
import bangkit.robbyyehezkiel.androidintermediate.view.activity.AuthenticateActivity

class LoginFragment : Fragment() {

    private val viewModel: AuthenticateViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref =
            UserPreferences.getPreferenceInstance((activity as AuthenticateActivity).dataStore)
        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]

        viewModel.let { vm ->
            vm.loginResponseResult.observe(viewLifecycleOwner) { login ->
                settingViewModel.setUserPreferences(
                    login.loginResult.token,
                    login.loginResult.userId,
                    login.loginResult.name,
                    viewModel.preferencesEmail.value ?: Constanta.preferenceDefaultValue
                )
            }
            vm.error.observe(viewLifecycleOwner) { error ->
                error?.let {
                    if (it.isNotEmpty()) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle(R.string.alert_failed)
                        builder.setMessage(it)
                        builder.setIcon(R.drawable.baseline_icon_warning_red)
                        builder.setPositiveButton("Ok") { dialog, _ ->
                            dialog.dismiss()
                        }
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.setCancelable(false)
                        alertDialog.show()
                    }
                }
            }
            vm.progressBar.observe(viewLifecycleOwner) { state ->
                binding.progressBar.visibility = state
            }
        }
        settingViewModel.getUserPreferences(Constanta.AuthPreferences.UserToken.name)
            .observe(viewLifecycleOwner) { token ->
                if (token != Constanta.preferenceDefaultValue) (activity as AuthenticateActivity).routeToMainActivity()
            }
        binding.btnAction.setOnClickListener {
            if ((binding.edEmail.text?.length ?: 0) <= 0) {
                binding.edEmail.error = getString(R.string.not_filled_email)
                binding.edEmail.requestFocus()
            } else if ((binding.edPassword.text?.length ?: 0) <= 0) {
                binding.edPassword.error = getString(R.string.password_not_field)
                binding.edPassword.requestFocus()
            } else if ((binding.edEmail.error?.length ?: 0) > 0) {
                binding.edEmail.requestFocus()
            } else if ((binding.edPassword.error?.length ?: 0) > 0) {
                binding.edPassword.requestFocus()
            } else {
                val email = binding.edEmail.text.toString()
                val password = binding.edPassword.text.toString()
                viewModel.authLogin(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {

            viewModel.error.postValue("")

            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameAuth, RegisterFragment(), RegisterFragment::class.java.simpleName)
                commit()
            }
        }
    }

    companion object {
        fun newInstance() = LoginFragment()
    }

}