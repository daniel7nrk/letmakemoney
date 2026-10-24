// Supabase Client Configuration
const SUPABASE_URL = 'https://berurshibbxpunyselmd.supabase.co';
const SUPABASE_ANON_KEY = 'sb_publishable_fRdux5ehpwczDqez9SLi_g_znW3Czty';

// Initialize supabase from CDN window.supabase
let client = null;
if (typeof window !== 'undefined' && window.supabase) {
  client = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
}

export const getSupabase = () => {
  if (!client && typeof window !== 'undefined' && window.supabase) {
    client = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
  }
  return client;
};
