import { FormEvent, useEffect, useState } from 'react';
import { usersApi } from '@/api/users';
import type { UserProfile, AddressDto } from '@/types/api';
import { useAuthStore } from '@/store/auth';
import { extractErrorMessage } from '@/api/client';

const EMPTY_ADDRESS: AddressDto = {
  recipient: '',
  phone: '',
  zipCode: '',
  addressLine1: '',
  addressLine2: '',
};

export default function ProfilePage() {
  const { setProfile } = useAuthStore();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState('');
  const [address, setAddress] = useState<AddressDto>(EMPTY_ADDRESS);
  const [currentPwd, setCurrentPwd] = useState('');
  const [newPwd, setNewPwd] = useState('');
  const [msg, setMsg] = useState<string | null>(null);

  const load = () => {
    setLoading(true);
    usersApi
      .me()
      .then((u) => {
        setMe(u);
        setName(u.name);
        setAddress(u.defaultAddress ?? EMPTY_ADDRESS);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const handleNameSave = async (e: FormEvent) => {
    e.preventDefault();
    setMsg(null);
    try {
      const updated = await usersApi.updateProfile(name);
      setMe(updated);
      setProfile(updated.email, updated.name, updated.role);
      setMsg('이름이 변경되었습니다.');
    } catch (err) {
      setMsg(extractErrorMessage(err));
    }
  };

  const handleAddressSave = async (e: FormEvent) => {
    e.preventDefault();
    setMsg(null);
    try {
      const updated = await usersApi.updateAddress(address);
      setMe(updated);
      setMsg('기본 배송지가 저장되었습니다.');
    } catch (err) {
      setMsg(extractErrorMessage(err));
    }
  };

  const handlePasswordSave = async (e: FormEvent) => {
    e.preventDefault();
    setMsg(null);
    if (newPwd.length < 8) {
      setMsg('새 비밀번호는 8자 이상이어야 합니다.');
      return;
    }
    try {
      await usersApi.updatePassword(currentPwd, newPwd);
      setCurrentPwd('');
      setNewPwd('');
      setMsg('비밀번호가 변경되었습니다.');
    } catch (err) {
      setMsg(extractErrorMessage(err));
    }
  };

  if (loading) return <p className="text-gray-500">불러오는 중...</p>;
  if (!me) return null;

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold">내 정보</h1>
      {msg && (
        <div className="rounded-md bg-blue-50 p-3 text-sm text-blue-800">
          {msg}
        </div>
      )}

      <section className="card p-4">
        <h2 className="mb-3 font-bold">계정</h2>
        <form onSubmit={handleNameSave} className="space-y-3">
          <div>
            <label className="label">이메일</label>
            <input className="input" value={me.email} disabled />
          </div>
          <div>
            <label className="label">권한</label>
            <input className="input" value={me.role} disabled />
          </div>
          <div>
            <label className="label">이름</label>
            <input
              className="input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn-primary">
            이름 저장
          </button>
        </form>
      </section>

      <section className="card p-4">
        <h2 className="mb-3 font-bold">기본 배송지</h2>
        <form onSubmit={handleAddressSave} className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div>
            <label className="label">받는 사람</label>
            <input
              className="input"
              value={address.recipient}
              onChange={(e) =>
                setAddress({ ...address, recipient: e.target.value })
              }
              required
            />
          </div>
          <div>
            <label className="label">연락처</label>
            <input
              className="input"
              value={address.phone}
              onChange={(e) => setAddress({ ...address, phone: e.target.value })}
              required
            />
          </div>
          <div>
            <label className="label">우편번호</label>
            <input
              className="input"
              value={address.zipCode}
              onChange={(e) =>
                setAddress({ ...address, zipCode: e.target.value })
              }
              required
            />
          </div>
          <div>
            <label className="label">주소</label>
            <input
              className="input"
              value={address.addressLine1}
              onChange={(e) =>
                setAddress({ ...address, addressLine1: e.target.value })
              }
              required
            />
          </div>
          <div className="sm:col-span-2">
            <label className="label">상세주소</label>
            <input
              className="input"
              value={address.addressLine2 ?? ''}
              onChange={(e) =>
                setAddress({ ...address, addressLine2: e.target.value })
              }
            />
          </div>
          <div className="sm:col-span-2">
            <button type="submit" className="btn-primary">
              배송지 저장
            </button>
          </div>
        </form>
      </section>

      <section className="card p-4">
        <h2 className="mb-3 font-bold">비밀번호 변경</h2>
        <form onSubmit={handlePasswordSave} className="space-y-3">
          <div>
            <label className="label">현재 비밀번호</label>
            <input
              type="password"
              className="input"
              value={currentPwd}
              onChange={(e) => setCurrentPwd(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="label">새 비밀번호 (8자 이상)</label>
            <input
              type="password"
              className="input"
              value={newPwd}
              onChange={(e) => setNewPwd(e.target.value)}
              minLength={8}
              required
            />
          </div>
          <button type="submit" className="btn-primary">
            비밀번호 변경
          </button>
        </form>
      </section>
    </div>
  );
}