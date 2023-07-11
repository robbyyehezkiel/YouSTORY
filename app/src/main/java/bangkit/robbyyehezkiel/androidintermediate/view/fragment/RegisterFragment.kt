package bangkit.robbyyehezkiel.androidintermediate.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.AuthenticateViewModel
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private val viewModel: AuthenticateViewModel by activityViewModels()
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.let { vm ->
            vm.registerResponseResult.observe(viewLifecycleOwner) { register ->
                if (!register.error) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(R.string.alert_success)
                    builder.setMessage(R.string.alert_register)
                    builder.setIcon(R.drawable.baseline_icon_warning_green)
                    builder.setPositiveButton("Ok") { _, _ ->
                        goToLogin()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.toast_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
            vm.error.observe(viewLifecycleOwner) { error ->
                if (error.isNotEmpty()) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(R.string.alert_failed)
                    builder.setMessage(error)
                    builder.setIcon(R.drawable.baseline_icon_warning_red)
                    builder.setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
            vm.progressBar.observe(viewLifecycleOwner) { state ->
                binding.loading.visibility = state
            }
        }
        binding.btnLogin.setOnClickListener {
            goToLogin()
        }
        binding.btnAction.setOnClickListener {
            if ((binding.edName.text?.length ?: 0) <= 0) {
                binding.edName.error = getString(R.string.name_not_field)
                binding.edName.requestFocus()
            } else if ((binding.edEmail.text?.length ?: 0) <= 0) {
                binding.edEmail.error = getString(R.string.not_filled_email)
                binding.edEmail.requestFocus()
            } else if ((binding.edPassword.text?.length ?: 0) <= 0) {
                binding.edPassword.error = getString(R.string.password_not_field)
                binding.edPassword.requestFocus()
            }

            else if ((binding.edEmail.error?.length ?: 0) > 0) {
                binding.edEmail.requestFocus()
            } else if ((binding.edPassword.error?.length ?: 0) > 0) {
                binding.edPassword.requestFocus()
            } else if ((binding.edName.error?.length ?: 0) > 0) {
                binding.edName.requestFocus()
            }

            else {
                val name = binding.edName.text.toString()
                val email = binding.edEmail.text.toString()
                val password = binding.edPassword.text.toString()
                viewModel.authRegister(name, email, password)
            }
        }
    }

    private fun goToLogin() {
        viewModel.error.postValue("")
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.frameAuth, LoginFragment(), LoginFragment::class.java.simpleName)
            commit()
        }
    }

}