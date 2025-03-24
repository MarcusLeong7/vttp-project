// For JWT Token
export interface User {
  email: string;
  token: string;
}

// For Weight Logging
export interface WeightLog {
  id: number;
  weight: number;
  date: string;
  notes?: string;
}
