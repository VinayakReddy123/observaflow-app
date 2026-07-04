export interface User{
    email: string
    tenantId: string
}

export interface LoginRequest{
    email: string
    password: string
}

export interface RegisterRequest{
    email: string
    password: string
    tenantId: string
}