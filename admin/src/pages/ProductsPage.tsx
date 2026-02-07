import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Pencil, Trash2, Plus } from 'lucide-react';
import {
  getProducts,
  createProduct,
  updateProduct,
  deleteProduct,
  type Product,
  type ProductFormData,
} from '@/api/products';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';

// Zod 검증 스키마
const productSchema = z.object({
  name: z
    .string()
    .min(1, '상품명을 입력하세요')
    .max(100, '상품명은 100자 이하여야 합니다'),
  description: z
    .string()
    .min(1, '설명을 입력하세요')
    .max(500, '설명은 500자 이하여야 합니다'),
  price: z
    .number({ message: '가격을 입력하세요' })
    .min(1, '가격은 1p 이상이어야 합니다'),
  stock: z
    .number({ message: '재고를 입력하세요' })
    .min(0, '재고는 0 이상이어야 합니다'),
  isActive: z.boolean(),
});

type ProductFormValues = z.infer<typeof productSchema>;

export default function ProductsPage() {
  const queryClient = useQueryClient();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [productToDelete, setProductToDelete] = useState<Product | null>(null);

  // 상품 목록 조회
  const { data: products, isLoading, error } = useQuery({
    queryKey: ['products'],
    queryFn: getProducts,
  });

  // React Hook Form 설정
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<ProductFormValues>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      name: '',
      description: '',
      price: 0,
      stock: 0,
      isActive: true,
    },
  });

  const isActive = watch('isActive');

  // 상품 생성 Mutation
  const createMutation = useMutation({
    mutationFn: createProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setIsDialogOpen(false);
      reset();
    },
    onError: (error: any) => {
      const message = error.response?.data?.error?.message || '상품 생성에 실패했습니다';
      alert(message);
    },
  });

  // 상품 수정 Mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ProductFormData }) =>
      updateProduct(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setIsDialogOpen(false);
      setEditingProduct(null);
      reset();
    },
    onError: (error: any) => {
      const message = error.response?.data?.error?.message || '상품 수정에 실패했습니다';
      alert(message);
    },
  });

  // 상품 삭제 Mutation
  const deleteMutation = useMutation({
    mutationFn: deleteProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setDeleteConfirmOpen(false);
      setProductToDelete(null);
    },
    onError: (error: any) => {
      const message =
        error.response?.data?.error?.message || '상품 삭제에 실패했습니다';
      if (error.response?.data?.error?.code === 'PRODUCT_HAS_ORDERS') {
        alert('주문 내역이 있어 삭제할 수 없습니다');
      } else {
        alert(message);
      }
      setDeleteConfirmOpen(false);
      setProductToDelete(null);
    },
  });

  // 상품 추가 버튼 클릭
  const handleAdd = () => {
    setEditingProduct(null);
    reset({
      name: '',
      description: '',
      price: 0,
      stock: 0,
      isActive: true,
    });
    setIsDialogOpen(true);
  };

  // 수정 버튼 클릭
  const handleEdit = (product: Product) => {
    setEditingProduct(product);
    reset({
      name: product.name,
      description: product.description || '',
      price: product.price,
      stock: product.stock,
      isActive: product.isActive,
    });
    setIsDialogOpen(true);
  };

  // 삭제 버튼 클릭
  const handleDeleteClick = (product: Product) => {
    setProductToDelete(product);
    setDeleteConfirmOpen(true);
  };

  // 삭제 확인
  const handleDeleteConfirm = () => {
    if (productToDelete) {
      deleteMutation.mutate(productToDelete.id);
    }
  };

  // 폼 제출
  const onSubmit = (data: ProductFormValues) => {
    if (editingProduct) {
      updateMutation.mutate({ id: editingProduct.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">상품 관리</h1>
        <Button onClick={handleAdd}>
          <Plus className="mr-2 h-4 w-4" />
          상품 추가
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>상품 목록</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading && (
            <div className="space-y-2">
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
            </div>
          )}

          {error && (
            <div className="text-center py-8 text-red-500">
              상품 목록을 불러오는데 실패했습니다
            </div>
          )}

          {!isLoading && !error && products && products.length === 0 && (
            <div className="text-center py-8 text-muted-foreground">
              등록된 상품이 없습니다
            </div>
          )}

          {!isLoading && !error && products && products.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>상품명</TableHead>
                  <TableHead>설명</TableHead>
                  <TableHead className="text-right">가격</TableHead>
                  <TableHead className="text-right">재고</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">액션</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {products.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell className="font-medium">{product.id}</TableCell>
                    <TableCell>{product.name}</TableCell>
                    <TableCell className="max-w-xs truncate">
                      {product.description || '-'}
                    </TableCell>
                    <TableCell className="text-right">
                      {product.price.toLocaleString()}p
                    </TableCell>
                    <TableCell className="text-right">
                      {product.stock === 0 ? (
                        <Badge variant="destructive">품절</Badge>
                      ) : (
                        <span>{product.stock.toLocaleString()}</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={product.isActive ? 'default' : 'secondary'}
                      >
                        {product.isActive ? 'ACTIVE' : 'INACTIVE'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleEdit(product)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleDeleteClick(product)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* 상품 추가/수정 Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>
              {editingProduct ? '상품 수정' : '상품 추가'}
            </DialogTitle>
            <DialogDescription>
              {editingProduct
                ? '상품 정보를 수정합니다'
                : '새로운 상품을 추가합니다'}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">상품명</Label>
              <Input
                id="name"
                {...register('name')}
                placeholder="상품명을 입력하세요"
              />
              {errors.name && (
                <p className="text-sm text-red-500">{errors.name.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">설명</Label>
              <Textarea
                id="description"
                {...register('description')}
                placeholder="상품 설명을 입력하세요"
                rows={3}
              />
              {errors.description && (
                <p className="text-sm text-red-500">
                  {errors.description.message}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="price">가격</Label>
              <Input
                id="price"
                type="number"
                {...register('price', { valueAsNumber: true })}
                placeholder="가격을 입력하세요"
              />
              {errors.price && (
                <p className="text-sm text-red-500">{errors.price.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="stock">재고</Label>
              <Input
                id="stock"
                type="number"
                {...register('stock', { valueAsNumber: true })}
                placeholder="재고를 입력하세요"
              />
              {errors.stock && (
                <p className="text-sm text-red-500">{errors.stock.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="isActive">상태</Label>
              <Select
                value={isActive ? 'true' : 'false'}
                onValueChange={(value) => setValue('isActive', value === 'true')}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="true">ACTIVE</SelectItem>
                  <SelectItem value="false">INACTIVE</SelectItem>
                </SelectContent>
              </Select>
              {errors.isActive && (
                <p className="text-sm text-red-500">
                  {errors.isActive.message}
                </p>
              )}
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setIsDialogOpen(false);
                  setEditingProduct(null);
                  reset();
                }}
              >
                취소
              </Button>
              <Button
                type="submit"
                disabled={
                  createMutation.isPending || updateMutation.isPending
                }
              >
                {editingProduct ? '수정' : '추가'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* 삭제 확인 Dialog */}
      <Dialog open={deleteConfirmOpen} onOpenChange={setDeleteConfirmOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>상품 삭제</DialogTitle>
            <DialogDescription>
              이 상품을 삭제하시겠습니까?
              <br />
              주문 내역이 있는 상품은 삭제할 수 없습니다.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setDeleteConfirmOpen(false);
                setProductToDelete(null);
              }}
            >
              취소
            </Button>
            <Button
              variant="destructive"
              onClick={handleDeleteConfirm}
              disabled={deleteMutation.isPending}
            >
              삭제
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
