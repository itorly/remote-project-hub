import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useAuthMutations } from '../api/hooks';
import { useAuth } from '../hooks/useAuth';

interface LoginForm {
  email: string;
  password: string;
  remember: boolean;
}

export const LoginPage = () => {
  const { register, handleSubmit } = useForm<LoginForm>({
    defaultValues: { remember: true }
  });
  const { login } = useAuthMutations();
  const { applyAuth } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = (location.state as { from?: Location })?.from?.pathname ?? '/organizations';

  const onSubmit = async (data: LoginForm) => {
    const response = await login.mutateAsync({ email: data.email, password: data.password });
    applyAuth(response, data.remember ? 'local' : 'memory');
    navigate(redirectTo, { replace: true });
  };

  return (
    <div className="card" style={{ maxWidth: 520, margin: '0 auto' }}>
      <h2>Login</h2>
      <p className="helper-text">
        Use the toggle below to decide whether your token stays only in memory (safer on shared devices) or in
        localStorage (convenient for returning sessions).
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="stack">
        <div className="form-group">
          <label className="label" htmlFor="email">
            Email
          </label>
          <input id="email" className="input" type="email" {...register('email', { required: true })} />
        </div>
        <div className="form-group">
          <label className="label" htmlFor="password">
            Password
          </label>
          <input id="password" className="input" type="password" {...register('password', { required: true })} />
        </div>
        <label className="inline" style={{ gap: 8 }}>
          <input type="checkbox" {...register('remember')} /> Keep me signed in (localStorage)
        </label>
        {login.isError && <p className="helper-text">Login failed. Check your email/password.</p>}
        <button className="btn btn-primary" type="submit" disabled={login.isPending}>
          {login.isPending ? 'Signing in…' : 'Login'}
        </button>
      </form>
      <p>
        No account? <Link to="/register">Create one</Link>
      </p>
    </div>
  );
};
