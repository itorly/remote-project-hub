import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useAuthMutations } from '../api/hooks';
import { useAuth } from '../hooks/useAuth';

interface RegisterForm {
  email: string;
  password: string;
  displayName: string;
  timezone?: string;
  remember: boolean;
}

export const RegisterPage = () => {
  const { register, handleSubmit } = useForm<RegisterForm>({
    defaultValues: { remember: true }
  });
  const { register: registerMutation } = useAuthMutations();
  const { applyAuth } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (data: RegisterForm) => {
    const response = await registerMutation.mutateAsync({
      email: data.email,
      password: data.password,
      displayName: data.displayName,
      timezone: data.timezone
    });
    applyAuth(response, data.remember ? 'local' : 'memory');
    navigate('/organizations', { replace: true });
  };

  return (
    <div className="card" style={{ maxWidth: 520, margin: '0 auto' }}>
      <h2>Create your account</h2>
      <p className="helper-text">
        Use the "Keep me signed in" toggle if you want the JWT stored in localStorage. Leave it off for
        in-memory storage that clears on refresh.
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="stack">
        <div className="form-group">
          <label className="label" htmlFor="displayName">
            Display name
          </label>
          <input id="displayName" className="input" {...register('displayName', { required: true })} />
        </div>
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
          <input id="password" className="input" type="password" {...register('password', { required: true, minLength: 6 })} />
        </div>
        <div className="form-group">
          <label className="label" htmlFor="timezone">
            Timezone (optional)
          </label>
          <input id="timezone" className="input" placeholder="e.g., Asia/Tokyo" {...register('timezone')} />
        </div>
        <label className="inline" style={{ gap: 8 }}>
          <input type="checkbox" {...register('remember')} /> Keep me signed in (localStorage)
        </label>
        {registerMutation.isError && <p className="helper-text">Registration failed. Try again.</p>}
        <button className="btn btn-primary" type="submit" disabled={registerMutation.isPending}>
          {registerMutation.isPending ? 'Creating...' : 'Register'}
        </button>
      </form>
      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
};
